db = db.getSiblingDB('cost_calc');

// Create application user
db.createUser({
    user: 'cost_calc_user',
    pwd: 'cost_calc_password',
    roles: [
        {
            role: 'readWrite',
            db: 'cost_calc'
        }
    ]
});

// Create collections
db.createCollection('users');
db.createCollection('services');
db.createCollection('materials');
db.createCollection('clients');
db.createCollection('calculations'); 