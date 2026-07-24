# Story: Chargepoint CRUD with Geospatial Location

## User Story
As a **Vendor**
I want to **add, update, and remove chargepoints with location data**
So that **my charging stations are registered with accurate positioning**

## Acceptance Criteria
- [ ] Given a vendor, When they create a chargepoint with name, group, price, location, vendor_id, Then the chargepoint is created
- [ ] Given a chargepoint, When updated, Then name, group, price, location, availability are modified
- [ ] Given a chargepoint, When removed (soft-delete), Then historical sessions are preserved
- [ ] Given chargepoint creation, When location is provided as lat/lng, Then it's stored as PostGIS GEOGRAPHY(Point, 4326)
- [ ] Given a vendor user, When they query chargepoints, Then only their vendor's chargepoints are returned

## Technical Notes
- Location: latitude/longitude in decimal degrees, WGS 84
- Unit price stored as integer (tenths of cents)
- PostGIS spatial index created on location column
- Soft-delete via deleted_at timestamp

## Dependencies
- Story 001-002 (Vendor user registration)
