# Transformation Engine

Normalizes vendor JSON payloads.

## Mapping Engine
The engine uses nested dot-notation mappings (e.g. `"email": "customer.email"`) to traverse source payloads, map target shapes, and fill default constants without calling complex external libraries.
