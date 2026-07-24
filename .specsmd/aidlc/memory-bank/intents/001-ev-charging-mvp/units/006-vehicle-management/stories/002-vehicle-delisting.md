# Story: Vehicle De-listing and Re-registration

## User Story
As a **Customer**
I want to **de-list my vehicle and allow another customer to register the same plate**
So that **I can manage my vehicle changes and the plate can be reused**

## Acceptance Criteria
- [ ] Given a customer with a vehicle, When they de-list it, Then the vehicle status becomes DE_LISTED
- [ ] Given a de-listed vehicle, When viewed, Then it no longer appears in the customer's active vehicle list
- [ ] Given a de-listed vehicle, When historical sessions are queried, Then the record remains intact
- [ ] Given a de-listed plate, When a different customer registers it, Then registration succeeds with new owner

## Dependencies
- Story 006-001 (Vehicle registration)
