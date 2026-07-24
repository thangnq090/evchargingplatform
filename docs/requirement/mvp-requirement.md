Principal AI Engineer: Solution Design Challenge
This exercise is designed to simulate the early stages of developing a production backend service. It is intentionally open-ended and focuses on your technical judgement, design decisions, and engineering process.
We are interested not only in the solution you deliver, but also in how you arrive at it.

Expectations
There is no single right solution. This exercise is designed to understand how you approach technical problems, apply your experience, and make engineering decisions.
We expect you to use AI throughout the exercise. We are interested in how you collaborate with AI and how you apply your own judgement when evaluating its output.
We value sound engineering judgement and well-reasoned trade-offs over completeness.
We do not expect every requirement to be fully implemented. Explain what you choose to build, simplify, defer, and why.
We do not expect perfection. Show your decisions, investigations, and iterations. Do not clean up your history. Your development journey is part of the assessment.

Scope of the Exercise
Approach this exercise as the architect and builder of a backend service that will become part of a production platform.
Although a user interface is not required, you should consider the needs of client applications when designing your APIs.
Use your judgement to decide what to implement fully, what to simplify, and what to document as future work. The implementation should demonstrate your approach and validate your design decisions.
Where requirements are incomplete, make sensible assumptions and document them. If you choose not to implement part of the solution, explain your reasoning and how you would approach it in a production system.
Consider how your solution would evolve as the platform grows.

Deliverables
Provide the following:

Solution
Access to a Git repository containing the solution backend.
• A dockerised solution.
• A REST API with authentication and API documentation (using Swagger or another documentation framework of your choice).
• Full-text search support.
• A simple way to populate the database with test data (for example, SQL scripts executed during application startup).

Design Documentation
• Overall architecture.
Significant design decisions, alternatives considered, and the reasoning behind the chosen approach.
Key assumptions made where requirements are incomplete.
• Areas intentionally simplified or deferred.
Relevant non-functional aspects of the solution and any important considerations, decisions, and trade-offs.
• Productisation considerations, including additional technical, operational, and architectural work required to move the solution into production.
Where your solution introduces additional components or technologies, explain the challenges they introduce and how you address them.

Testing
• Test plans.
• Automated tests where appropriate.
Additional testing that should be considered.

AI Workflow
• AI transcript showing your interaction with AI throughout the exercise.
• Any to-do list or tracking approach used (for example, notes, a task list, or repository issues).
• Highlights of key interactions or decisions.
Include enough context to understand how AI influenced your thinking, decisions, and implementation approach.

Productising the Solution
A summary of additional work you would undertake before taking this solution to production.

Presentation
A presentation of up to 15 minutes introducing your solution, explaining your design decisions, and walking us through your implementation.
Be prepared to discuss:
• alternative approaches considered
• limitations of your solution
• what you would change with additional time
This will be followed by discussion.

Setup
Use Spring Initializr to create the initial Spring Boot project structure with a configuration allowing you to manage entities in a relational database and access the data using REST APIS with authentication.

Entity Model
Create an entity model using the description below. Key properties are listed on the first line of each entity.

Admin users
Properties: name, email.
• Sign up vendors, including inviting the original vendor user.
• View income over a period, optionally filtered by vendor.
• Set the markup added to the vendor's unit price.
• Reset credentials for users where appropriate.

Vendors
Properties: name, account number.
• Have one or more users who can manage vendor-owned resources according to their assigned permissions.
• Own chargepoints and can add, update, and remove them.
• Require insights into income and charging activity, including the current month and breakdowns by chargepoint over recent days, weeks, and months.
• Can generate reports of charging sessions for a chargepoint on a given date.

Vendor users
Properties: name, email, vendor.
• Belong to exactly one vendor.
• Can manage vendor-owned resources according to their assigned permissions.

Chargepoints
Properties: unique identifier, group label, unit price, availability, vendor.
• Are owned by a vendor.
• Have a unique identifier displayed on the physical chargepoint for support purposes.
• May have a group label used by vendors to identify multiple chargepoints belonging together.
• Have a unit price in tenths of cents.
• May be temporarily unavailable.

Customer users
Properties: name, email, account number, phone number.
• Register with the platform.
• Have a phone number and an assigned account number.
• Perform charging sessions using the marked-up unit price.
• Can view their charging session history and totals by month, including the current partial month.
• Billing is handled elsewhere.
• Own one or more vehicles and maintain them themselves.
• Can de-list vehicles (for example, following disposal or sale).
• If a vehicle is later owned by another customer, it may be registered again with the same plate.

Vehicles
Properties: registration plate, RFID number, customer.
• Are owned by a customer.
• Can be identified automatically by RFID or plate detection.
• Can also be selected manually by the customer.
• If an RFID is available during a manual selection, it may be associated with the vehicle for future identification.

Charging sessions
Properties: start, end, vehicle, chargepoint, marked-up unit rate, error code, total energy, total charged.
• Belong to the month in which they start.
• Use the unit price applicable at the session start time.
• Record an error code if the charging session is partially or fully unsuccessful; otherwise, the error code is empty.
• Record the total energy delivered (kWh).
• Record the total amount charged to the customer.

Make sensible assumptions about the data model and the properties of the entities where requirements are incomplete.

REST API
The solution should provide a REST API with authentication and API documentation (using Swagger or another documentation framework of your choice).
Design the API to support the capabilities described by the entity model while considering the needs of client applications.
Select an authentication approach appropriate for the solution and explain your decision.

Full-Text Search
Provide an endpoint allowing administrators to search charging sessions using session, customer, or vehicle information (for example, registration plates, customer account numbers, or charging session error codes).
The search should support partial matches. For example, the search term AUD should match vehicles with the registration plates AUD186 and AUD994.
