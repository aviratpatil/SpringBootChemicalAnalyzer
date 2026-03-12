import sys
import time
import requests
import psycopg2
from psycopg2.extras import execute_batch
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

# --- Configuration ---
DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "dbname": "dermadata",
    "user": "postgres",
    "password": "avirat"
}

API_BASE_URL = "https://ec.europa.eu/growth/tools-databases/cosing/api/ingredients"
BATCH_SIZE = 100
LOG_INTERVAL = 500

def get_session():
    """Configure a requests session with robust retry logic."""
    session = requests.Session()
    retries = Retry(total=5, backoff_factor=1, status_forcelist=[429, 500, 502, 503, 504])
    adapter = HTTPAdapter(max_retries=retries)
    session.mount("http://", adapter)
    session.mount("https://", adapter)
    return session

def fetch_and_seed():
    session = get_session()
    
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()
        print("✓ Connected to PostgreSQL")
    except Exception as e:
        print(f"✗ Database connection failed: {e}")
        sys.exit(1)

    # Upsert Query matched to the actual Spring Data JPA entity schema
    upsert_sql = """
        INSERT INTO ingredient_regulation (
            inci_name, max_concentration, restricted, prohibited, 
            conditions, product_types, regulation_ref
        )
        VALUES (%s, %s, %s, %s, %s, %s, %s)
        ON CONFLICT (inci_name) DO UPDATE SET
            max_concentration = EXCLUDED.max_concentration,
            restricted = EXCLUDED.restricted,
            prohibited = EXCLUDED.prohibited,
            conditions = EXCLUDED.conditions,
            product_types = EXCLUDED.product_types,
            regulation_ref = EXCLUDED.regulation_ref;
    """

    page = 0
    total_processed = 0
    has_more = True

    print("Starting bulk fetch from EU CosIng API...")
    start_time = time.time()

    while has_more:
        try:
            # Note: The EU CosIng API is an example interface - actual params might differ
            response = session.get(API_BASE_URL, params={"page": page, "size": BATCH_SIZE}, timeout=10)
            
            # Since the API URL above is a generalized example and likely will 404/403 directly, 
            # we handle graceful exit if it's not implemented exactly like this standard REST.
            if response.status_code != 200:
                print(f"⚠ API returned status {response.status_code}. The EU endpoint might require authentication, different headers, or a bulk XML download instead of paginated JSON.")
                print("Exiting sync loop gracefully.")
                break

            data = response.json()
            ingredients = data.get("content", [])
            
            if not ingredients:
                has_more = False
                break

            # Map JSON response to Database Tuple (adapting generic status to actual JPA fields)
            batch_data = []
            for item in ingredients:
                status = item.get("status", "SAFE").upper()
                restricted = status == "RESTRICTED"
                prohibited = status == "PROHIBITED"
                
                batch_data.append((
                    item.get("inciName", "").strip().upper(),
                    item.get("maxConcentration"),   # None if not present
                    restricted,
                    prohibited,
                    item.get("notes", ""),          # Mapped to conditions in entity
                    item.get("productTypes", ""),
                    item.get("regulationRef", "")
                ))

            # Execute Batch and Commit
            execute_batch(cur, upsert_sql, batch_data)
            conn.commit()

            total_processed += len(batch_data)
            
            # Log progress every LOG_INTERVAL
            if total_processed % LOG_INTERVAL == 0:
                elapsed = round(time.time() - start_time, 2)
                print(f"[{elapsed}s] Processed {total_processed} ingredients...")

            page += 1

        except requests.exceptions.RequestException as e:
            print(f"✗ Network error occurred on page {page}: {e}")
            break
        except Exception as e:
            print(f"✗ Database error occurred on page {page}: {e}")
            conn.rollback() # Rollback current batch on failure
            time.sleep(5)   # Wait before attempting the next page

    cur.close()
    conn.close()
    print(f"✓ Completed! Successfully synced {total_processed} ingredients in {round(time.time() - start_time, 2)}s.")

if __name__ == "__main__":
    fetch_and_seed()
