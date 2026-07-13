#!/usr/bin/env python3
"""Fail when an ASCII STL contains boundary or non-manifold edges."""

from collections import Counter
from pathlib import Path
import sys


def vertices(path):
    with path.open(encoding="utf-8") as stl:
        for line in stl:
            fields = line.split()
            if fields and fields[0] == "vertex":
                yield tuple(float(value) for value in fields[1:4])


def edge_counts(path):
    points = list(vertices(path))
    if not points or len(points) % 3:
        raise ValueError("expected a non-empty ASCII STL with triangular facets")

    counts = Counter()
    for index in range(0, len(points), 3):
        a, b, c = points[index:index + 3]
        counts.update((tuple(sorted((a, b))),
                       tuple(sorted((b, c))),
                       tuple(sorted((c, a)))))
    return counts


def main(paths):
    failed = False
    for name in paths:
        path = Path(name)
        try:
            counts = edge_counts(path)
        except (OSError, UnicodeError, ValueError) as error:
            print(f"{path}: {error}", file=sys.stderr)
            failed = True
            continue

        boundary = sum(count == 1 for count in counts.values())
        non_manifold = sum(count > 2 for count in counts.values())
        print(f"{path}: boundary={boundary}, non_manifold={non_manifold}")
        failed |= boundary != 0 or non_manifold != 0
    return 1 if failed else 0


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(f"usage: {sys.argv[0]} MODEL.stl [...]", file=sys.stderr)
        raise SystemExit(2)
    raise SystemExit(main(sys.argv[1:]))
