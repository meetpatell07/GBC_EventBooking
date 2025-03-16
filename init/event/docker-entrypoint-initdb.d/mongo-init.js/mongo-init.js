print('START');

db = db.getSiblingDB('event-service');

db.createUser(
    {
        user:'admin',
        pwd:'password',
        roles: [ {role: 'readWrite',db: 'event-service'}]
    }
);

db.createCollection('events');

print('END');