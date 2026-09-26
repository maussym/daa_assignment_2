"""Make two simple figures from the benchmark CSV."""

import csv
from pathlib import Path

import matplotlib.pyplot as plt


root = Path(__file__).parent
with (root / "results/tables/results.csv").open(newline="") as file:
    rows = list(csv.DictReader(file))

panels = [
    ("Random access", ("random_access",)),
    ("Search", ("search",)),
    ("Insertion", ("insert_front", "insert_middle")),
    ("Priority queue", ("heap_insert", "heap_extract")),
]

for column, ylabel, filename in [
    ("average_time_ms", "Average time (ms)", "time_vs_n.png"),
    ("average_work", "Average work count", "work_vs_n.png"),
]:
    fig, axes = plt.subplots(2, 2, figsize=(12, 8))
    for ax, (title, workloads) in zip(axes.flat, panels):
        for workload in workloads:
            structures = sorted({row["structure"] for row in rows if row["workload"] == workload})
            for structure in structures:
                data = sorted(
                    (row for row in rows if row["workload"] == workload
                     and row["structure"] == structure),
                    key=lambda row: int(row["n"]),
                )
                label = structure if len(workloads) == 1 else f"{structure} {workload.split('_')[-1]}"
                ax.plot([int(row["n"]) for row in data],
                        [float(row[column]) for row in data], marker="o", label=label)
        ax.set_title(title)
        ax.set_xlabel("Initial size n")
        ax.set_ylabel(ylabel)
        ax.set_xscale("log")
        ax.set_yscale("log")
        ax.grid(True, which="both", alpha=0.3)
        ax.legend(fontsize=8)
    fig.tight_layout()
    output = root / "results/plots" / filename
    output.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(output, dpi=160)
    plt.close(fig)
    print(f"Saved {output}")
