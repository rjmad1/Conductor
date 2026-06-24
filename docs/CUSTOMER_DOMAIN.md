# Customer Domain Overview

The Customer Registry and Customer 360 platform provides a unified profile, contact management, and consent tracking registry.

## Core Aggregates

### 1. Customer
The root entity containing profile status, source metadata, and display fields. First name and last name are encrypted at rest using AES-256-GCM.

### 2. Contact Management
Customer contacts support multiple types (Email, Phone, WhatsApp, Address, Social, Custom). Only one primary contact is allowed per channel type. Normalized values are hashed via SHA-256 for fast resolution checks.

### 3. Consent Management
An append-only consent ledger mapping communication channels to legal basis and opt-in/opt-out status.

### 4. Segment & Tag Systems
- **Tags:** Categorized tenant-scoped labels.
- **Segments:** Dynamic, Static, or Tag-based lists representing groups of customers.

### 5. Timeline (Customer 360)
An append-only chronological log of all interactions and updates regarding the customer profile.
