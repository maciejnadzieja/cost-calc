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
db.createCollection('services');
db.createCollection('materials');
db.createCollection('user_settings');

const userId = UUID("00000000-0000-0000-0000-000000000000");

const materials = [
    { "_id": UUID(), "name": "Lawn Mowing", "description": "", "price": 50.00, "unit": "SQUARE_METER", "category": "Lawn Care", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Fertilizer", "description": "", "price": 20.00, "unit": "KILOGRAM", "category": "Lawn Care", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Weed Killer", "description": "", "price": 15.00, "unit": "LITER", "category": "Lawn Care", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Grass Seed", "description": "", "price": 25.00, "unit": "KILOGRAM", "category": "Lawn Care", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Topsoil", "description": "", "price": 10.00, "unit": "KILOGRAM", "category": "Gardening", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Mulch", "description": "", "price": 15.00, "unit": "KILOGRAM", "category": "Gardening", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Rose Bush", "description": "", "price": 30.00, "unit": "PIECE", "category": "Plants", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Oak Tree", "description": "", "price": 100.00, "unit": "PIECE", "category": "Plants", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Garden Hose", "description": "", "price": 40.00, "unit": "PIECE", "category": "Tools", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Shovel", "description": "", "price": 25.00, "unit": "PIECE", "category": "Tools", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Work Gloves", "description": "", "price": 10.00, "unit": "PACK", "category": "Tools", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Pesticide", "description": "", "price": 22.00, "unit": "LITER", "category": "Pest Control", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Hedge Trimmer", "description": "", "price": 150.00, "unit": "PIECE", "category": "Tools", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Flower Pot", "description": "", "price": 12.00, "unit": "PIECE", "category": "Gardening", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Gardening Soil", "description": "", "price": 8.00, "unit": "KILOGRAM", "category": "Gardening", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Watering Can", "description": "", "price": 18.00, "unit": "PIECE", "category": "Tools", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Pruning Shears", "description": "", "price": 20.00, "unit": "PIECE", "category": "Tools", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Wheelbarrow", "description": "", "price": 80.00, "unit": "PIECE", "category": "Tools", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Rake", "description": "", "price": 22.00, "unit": "PIECE", "category": "Tools", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Lawn Edger", "description": "", "price": 90.00, "unit": "PIECE", "category": "Tools", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() }
];

const services = [
    { "_id": UUID(), "name": "Lawn Mowing", "description": "Weekly lawn mowing service", "price": 0.50, "workUnit": "SQUARE_METER", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Garden Design", "description": "Custom garden design and planning", "price": 100.00, "workUnit": "HOUR", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Tree Trimming", "description": "", "price": 200.00, "workUnit": "PIECE", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Hedge Trimming", "description": "", "price": 1.50, "workUnit": "LINEAR_METER", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Weed Control", "description": "", "price": 0.20, "workUnit": "SQUARE_METER", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Fertilization", "description": "", "price": 0.30, "workUnit": "SQUARE_METER", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Pest Control", "description": "", "price": 0.25, "workUnit": "SQUARE_METER", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Garden Cleanup", "description": "", "price": 50.00, "workUnit": "HOUR", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Planting", "description": "", "price": 25.00, "workUnit": "HOUR", "userId": userId, "createdAt": new Date(), "updatedAt": new
Date() },
    { "_id": UUID(), "name": "Irrigation System Installation", "description": "", "price": 500.00, "workUnit": "PIECE", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Leaf Removal", "description": "", "price": 40.00, "workUnit": "HOUR", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Snow Removal", "description": "", "price": 60.00, "workUnit": "HOUR", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Hardscaping", "description": "Patios, walkways, retaining walls", "price": 150.00, "workUnit": "SQUARE_METER", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Landscape Lighting", "description": "", "price": 80.00, "workUnit": "PIECE", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Soil Testing", "description": "", "price": 30.00, "workUnit": "PIECE", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Aeration", "description": "", "price": 0.15, "workUnit": "SQUARE_METER", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Overseeding", "description": "", "price": 0.20, "workUnit": "SQUARE_METER", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Mulching", "description": "", "price": 1.00, "workUnit": "SQUARE_METER", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() },
    { "_id": UUID(), "name": "Gutter Cleaning", "description": "", "price": 100.00, "workUnit": "PIECE", "userId": userId, "createdAt": new Date(), "updatedat": new Date() },
    { "_id": UUID(), "name": "Pressure Washing", "description": "", "price": 0.75, "workUnit": "SQUARE_METER", "userId": userId, "createdAt": new Date(), "updatedAt": new Date() }
];

db.materials.insertMany(materials);
db.services.insertMany(services);

db.user_settings.insertOne({
    "_id": userId,
    "defaultCurrency": "USD",
    "language": "en"
});
