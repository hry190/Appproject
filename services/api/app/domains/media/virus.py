from __future__ import annotations

import socket
import struct
from dataclasses import dataclass
from typing import Protocol

from app.core.config import Settings


@dataclass(frozen=True, slots=True)
class VirusScanVerdict:
    clean: bool
    detector_version: str
    signature: str | None = None


class VirusScanner(Protocol):
    def scan(self, data: bytes) -> VirusScanVerdict: ...


class DevelopmentVirusScanner:
    """EICAR-only guard for tests; production configuration forbids this scanner."""

    EICAR_MARKER = b"EICAR-STANDARD-ANTIVIRUS-TEST-FILE"

    def scan(self, data: bytes) -> VirusScanVerdict:
        infected = self.EICAR_MARKER in data
        return VirusScanVerdict(
            clean=not infected,
            detector_version="development-eicar-pattern-v1",
            signature="EICAR-Test-Signature" if infected else None,
        )


class ClamAVVirusScanner:
    """Streams bytes through clamd INSTREAM over a private TCP network."""

    def __init__(self, host: str, port: int) -> None:
        self.host = host
        self.port = port

    def scan(self, data: bytes) -> VirusScanVerdict:
        with socket.create_connection((self.host, self.port), timeout=10) as connection:
            connection.sendall(b"zINSTREAM\0")
            for offset in range(0, len(data), 1024 * 1024):
                chunk = data[offset : offset + 1024 * 1024]
                connection.sendall(struct.pack(">I", len(chunk)))
                connection.sendall(chunk)
            connection.sendall(struct.pack(">I", 0))
            reply = bytearray()
            while True:
                received = connection.recv(4096)
                if not received:
                    break
                reply.extend(received)
                if b"\0" in received:
                    break
        decoded = bytes(reply).rstrip(b"\0").decode("utf-8", "replace")
        if decoded.endswith(" OK"):
            return VirusScanVerdict(clean=True, detector_version="clamd-instream")
        if decoded.endswith(" FOUND"):
            signature = decoded.rsplit(": ", 1)[-1].removesuffix(" FOUND")
            return VirusScanVerdict(
                clean=False,
                detector_version="clamd-instream",
                signature=signature,
            )
        raise RuntimeError("ClamAV returned an indeterminate response")


def build_virus_scanner(settings: Settings) -> VirusScanner:
    if settings.media_virus_scanner == "clamav":
        return ClamAVVirusScanner(settings.clamav_host, settings.clamav_port)
    return DevelopmentVirusScanner()
