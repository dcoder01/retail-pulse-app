#!/usr/bin/env python3
"""
RetailPulse Load Test Script
============================
TODO: This file is provided as a scaffold. The complete implementation is
      described in Section 10 of the README. Copy the full script from there
      and save it here as load_test.py in the project root.

Usage (once implemented):
    python3 load_test.py                          # Default: 60s, 10 RPS, localhost:8080
    python3 load_test.py --duration 120           # Run for 2 minutes
    python3 load_test.py --rps 25                 # 25 requests per second per worker
    python3 load_test.py --workers 5              # 5 concurrent worker threads
    python3 load_test.py --host http://localhost:8080
    python3 load_test.py --chaos                  # Exhaust P005 stock to trigger alerts

Requirements: Python 3.8+, no external libraries (standard library only).
"""
