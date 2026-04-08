"""Basic tests for the DataIngestor."""
import pandas as pd
from bet_analyzer.ingest import DataIngestor


def test_load_local_csv(tmp_path):
    """Ensure the CSV loader correctly reads a CSV file."""
    # Create a temporary CSV
    csv_path = tmp_path / "sample.csv"
    df = pd.DataFrame({"a": [1, 2], "b": [3, 4]})
    df.to_csv(csv_path, index=False)
    loaded = DataIngestor.load_local_csv(str(csv_path))
    assert loaded.equals(df)
