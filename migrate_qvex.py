from pathlib import Path

OLD = "com.movtery.zalithlauncher"
NEW = "com.myqvexzz.qvexlauncher"

ROOTS = [Path("QVEXLauncher/src"), Path("LWJGL/patches/src")]
EXTS = {".kt", ".java", ".xml", ".pro", ".c", ".h", ".cpp", ".gradle", ".kts", ".properties"}

files = []
for root in ROOTS:
    if root.exists():
        for p in root.rglob("*"):
            if p.is_file() and p.suffix in EXTS:
                try:
                    if OLD in p.read_text(encoding="utf-8"):
                        files.append(p)
                except (UnicodeDecodeError, OSError):
                    pass

moves = []
for root in ROOTS:
    for part in ("main/java", "test/java", "test/resources"):
        old = root / part / "com/movtery/zalithlauncher"
        if old.exists():
            new = root / part / "com/myqvexzz/qvexlauncher"
            moves.append((old, new))

print("QVEX MIGRATION DRY-RUN")
print("Files to update:", len(files))
print("Directories to move:", len(moves))
for old, new in moves:
    print(old, "->", new)
print("DRY-RUN ONLY. NO SOURCE FILES CHANGED.")
