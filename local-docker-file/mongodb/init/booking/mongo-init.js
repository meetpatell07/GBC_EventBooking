print('START');

db = db.getSiblingDB('booking-service');

db.createUser(
    {
        user:'admin',
        pwd:'password',
        roles: [ {role: 'readWrite',db: 'booking-service'}]
    }
);

db.createCollection('bookings');

print('END');