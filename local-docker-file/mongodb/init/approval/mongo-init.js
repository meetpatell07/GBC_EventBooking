print('START');

db = db.getSiblingDB('approval-service');

db.createUser(
    {
        user:'admin',
        pwd:'password',
        roles: [ {role: 'readWrite',db: 'approval-service'}]
    }
);

db.createCollection('approval');

print('END');