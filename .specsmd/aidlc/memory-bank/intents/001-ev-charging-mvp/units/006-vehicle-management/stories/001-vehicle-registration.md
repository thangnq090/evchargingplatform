# Story: Vehicle Registration with Plate and RFID

## User Story
As a **Customer**
I want to **register my vehicle with registration plate and optional RFID**
So that **my vehicle can be identified automatically during charging**

## Acceptance Criteria
- [ ] Given a customer, When they register a vehicle with plate, Then the vehicle is created under their ownership
- [ ] Given vehicle registration, When RFID is provided, Then it's associated for future auto-identification
- [ ] Given a vehicle, When RFID is detected at a charger, Then the vehicle is identified automatically
- [ ] Given manual session start, When RFID is available on the vehicle, Then it can be associated for future use

## Dependencies
- Story 001-003 (Customer registration)
