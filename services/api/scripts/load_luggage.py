from __future__ import annotations

import argparse
import asyncio
import math
import statistics
import time
from collections import Counter

import httpx


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Run a bounded smoke load against GET /v1/me/luggage."
    )
    parser.add_argument("--base-url", default="http://127.0.0.1:8010")
    parser.add_argument("--token", required=True, help="A test account access token")
    parser.add_argument("--requests", type=int, default=200)
    parser.add_argument("--concurrency", type=int, default=20)
    parser.add_argument("--p95-budget-ms", type=float, default=300.0)
    parser.add_argument(
        "--conditional",
        action="store_true",
        help="Reuse the warm-up ETag and exercise 304 responses",
    )
    args = parser.parse_args()
    if args.requests < 1 or args.concurrency < 1:
        parser.error("--requests and --concurrency must be positive")
    return args


def percentile(values: list[float], fraction: float) -> float:
    ordered = sorted(values)
    index = max(0, math.ceil(len(ordered) * fraction) - 1)
    return ordered[index]


async def run(args: argparse.Namespace) -> int:
    authorization = {"Authorization": f"Bearer {args.token}"}
    limits = httpx.Limits(
        max_connections=args.concurrency,
        max_keepalive_connections=args.concurrency,
    )
    timeout = httpx.Timeout(10.0)
    async with httpx.AsyncClient(
        base_url=args.base_url.rstrip("/"),
        headers=authorization,
        limits=limits,
        timeout=timeout,
    ) as client:
        warmup = await client.get("/v1/me/luggage")
        warmup.raise_for_status()
        request_headers = {}
        if args.conditional:
            etag = warmup.headers.get("etag")
            if etag is None:
                raise RuntimeError("warm-up response did not include ETag")
            request_headers["If-None-Match"] = etag

        semaphore = asyncio.Semaphore(args.concurrency)

        async def request_once() -> tuple[int, float]:
            async with semaphore:
                started = time.perf_counter()
                try:
                    response = await client.get(
                        "/v1/me/luggage",
                        headers=request_headers,
                    )
                    status = response.status_code
                except httpx.HTTPError:
                    status = 0
                elapsed_ms = (time.perf_counter() - started) * 1000
                return status, elapsed_ms

        started = time.perf_counter()
        results = await asyncio.gather(
            *(request_once() for _ in range(args.requests))
        )
        wall_seconds = time.perf_counter() - started

    statuses = Counter(status for status, _elapsed in results)
    latencies = [elapsed for _status, elapsed in results]
    expected = {304} if args.conditional else {200}
    unexpected = sum(count for status, count in statuses.items() if status not in expected)
    p95 = percentile(latencies, 0.95)
    print(f"requests={args.requests} concurrency={args.concurrency}")
    print(f"statuses={dict(sorted(statuses.items()))}")
    print(
        "latency_ms "
        f"p50={percentile(latencies, 0.50):.1f} "
        f"p95={p95:.1f} "
        f"mean={statistics.fmean(latencies):.1f}"
    )
    print(f"throughput_rps={args.requests / wall_seconds:.1f}")
    if unexpected:
        print(f"FAIL: {unexpected} unexpected responses")
        return 1
    if p95 > args.p95_budget_ms:
        print(f"FAIL: p95 exceeded {args.p95_budget_ms:.1f} ms budget")
        return 1
    print("PASS")
    return 0


def main() -> None:
    raise SystemExit(asyncio.run(run(parse_args())))


if __name__ == "__main__":
    main()
