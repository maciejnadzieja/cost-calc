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

// Development samples: the Docker entrypoint runs this script only for a fresh database.
// Items are embedded snapshots, matching the application's calculation model.
const calculations = [
    {
        _id: UUID("60000000-0000-0000-0000-000000000001"),
        name: "Weekly lawn care",
        description: "Services-only sample for a small garden.",
        userId: userId,
        currency: "USD",
        stages: [{
            name: "Routine maintenance",
            services: [
                { name: "Lawn Mowing", quantity: NumberDecimal("200"), unit: "SQUARE_METER", unitPrice: NumberDecimal("0.50"), type: "SERVICE" },
                { name: "Hedge Trimming", quantity: NumberDecimal("50"), unit: "LINEAR_METER", unitPrice: NumberDecimal("1.50"), type: "SERVICE" }
            ],
            materials: []
        }],
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: UUID("60000000-0000-0000-0000-000000000002"),
        name: "Spring planting supplies",
        description: "Materials-only sample for replenishing garden beds.",
        userId: userId,
        currency: "USD",
        stages: [{
            name: "Planting supplies",
            services: [],
            materials: [
                { name: "Rose Bush", quantity: NumberDecimal("3"), unit: "PIECE", unitPrice: NumberDecimal("30.00"), type: "MATERIAL" },
                { name: "Mulch", quantity: NumberDecimal("5"), unit: "KILOGRAM", unitPrice: NumberDecimal("15.00"), type: "MATERIAL" },
                { name: "Topsoil", quantity: NumberDecimal("2"), unit: "KILOGRAM", unitPrice: NumberDecimal("10.00"), type: "MATERIAL" }
            ]
        }],
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: UUID("60000000-0000-0000-0000-000000000003"),
        name: "Garden renovation",
        description: "Two-stage sample combining labour and materials.",
        userId: userId,
        currency: "USD",
        stages: [
            {
                name: "Preparation",
                services: [
                    { name: "Garden Cleanup", quantity: NumberDecimal("2.5"), unit: "HOUR", unitPrice: NumberDecimal("50.00"), type: "SERVICE" }
                ],
                materials: [
                    { name: "Topsoil", quantity: NumberDecimal("10"), unit: "KILOGRAM", unitPrice: NumberDecimal("10.00"), type: "MATERIAL" }
                ]
            },
            {
                name: "Planting and finishing",
                services: [
                    { name: "Planting", quantity: NumberDecimal("3"), unit: "HOUR", unitPrice: NumberDecimal("25.00"), type: "SERVICE" }
                ],
                materials: [
                    { name: "Rose Bush", quantity: NumberDecimal("4"), unit: "PIECE", unitPrice: NumberDecimal("30.00"), type: "MATERIAL" },
                    { name: "Mulch", quantity: NumberDecimal("5"), unit: "KILOGRAM", unitPrice: NumberDecimal("15.00"), type: "MATERIAL" },
                    { name: "Grass Seed", quantity: NumberDecimal("0.58"), unit: "KILOGRAM", unitPrice: NumberDecimal("25.00"), type: "MATERIAL" }
                ]
            }
        ],
        createdAt: new Date(),
        updatedAt: new Date()
    }
];
db.calculations.insertMany(calculations);

// Polish counterparts belong to a separate fixed-ID tester in the local realm.
const polishUserId = UUID("00000000-0000-0000-0000-000000000001");
const polishNames = {
    "Lawn Mowing": "Koszenie trawnika",
    "Fertilizer": "Nawóz",
    "Weed Killer": "Środek chwastobójczy",
    "Grass Seed": "Nasiona trawy",
    "Topsoil": "Ziemia ogrodowa",
    "Mulch": "Kora do ściółkowania",
    "Rose Bush": "Krzew róży",
    "Oak Tree": "Dąb",
    "Garden Hose": "Wąż ogrodowy",
    "Shovel": "Łopata",
    "Work Gloves": "Rękawice robocze",
    "Pesticide": "Środek ochrony roślin",
    "Hedge Trimmer": "Nożyce do żywopłotu",
    "Flower Pot": "Doniczka",
    "Gardening Soil": "Podłoże ogrodnicze",
    "Watering Can": "Konewka",
    "Pruning Shears": "Sekator",
    "Wheelbarrow": "Taczka",
    "Rake": "Grabie",
    "Lawn Edger": "Podkaszarka",
    "Garden Design": "Projektowanie ogrodu",
    "Tree Trimming": "Przycinanie drzew",
    "Hedge Trimming": "Przycinanie żywopłotu",
    "Weed Control": "Usuwanie chwastów",
    "Fertilization": "Nawożenie",
    "Pest Control": "Zwalczanie szkodników",
    "Garden Cleanup": "Porządkowanie ogrodu",
    "Planting": "Sadzenie roślin",
    "Irrigation System Installation": "Montaż systemu nawadniania",
    "Leaf Removal": "Grabienie liści",
    "Snow Removal": "Odśnieżanie",
    "Hardscaping": "Budowa ścieżek i tarasów",
    "Landscape Lighting": "Oświetlenie ogrodu",
    "Soil Testing": "Badanie gleby",
    "Aeration": "Aeracja trawnika",
    "Overseeding": "Dosiewanie trawy",
    "Mulching": "Ściółkowanie",
    "Gutter Cleaning": "Czyszczenie rynien",
    "Pressure Washing": "Mycie ciśnieniowe",
    "Lawn Care": "Pielęgnacja trawnika",
    "Gardening": "Ogrodnictwo",
    "Plants": "Rośliny",
    "Tools": "Narzędzia",
    "Weekly lawn care": "Cotygodniowa pielęgnacja trawnika",
    "Routine maintenance": "Regularna pielęgnacja",
    "Spring planting supplies": "Materiały do wiosennych nasadzeń",
    "Planting supplies": "Materiały do sadzenia",
    "Garden renovation": "Modernizacja ogrodu",
    "Preparation": "Przygotowanie",
    "Planting and finishing": "Sadzenie i wykończenie"
};
const polishDescriptions = {
    "": "",
    "Weekly lawn mowing service": "Cotygodniowe koszenie trawnika",
    "Custom garden design and planning": "Indywidualny projekt i planowanie ogrodu",
    "Patios, walkways, retaining walls": "Tarasy, ścieżki i murki oporowe",
    "Services-only sample for a small garden.": "Przykładowa wycena usług dla małego ogrodu.",
    "Materials-only sample for replenishing garden beds.": "Przykładowa wycena materiałów do uzupełnienia rabat.",
    "Two-stage sample combining labour and materials.": "Dwuetapowa wycena obejmująca robociznę i materiały."
};

db.materials.insertMany(materials.map(material => ({
    ...material,
    _id: UUID(),
    userId: polishUserId,
    name: polishNames[material.name],
    description: polishDescriptions[material.description],
    category: polishNames[material.category]
})));
db.services.insertMany(services.map(service => ({
    ...service,
    _id: UUID(),
    userId: polishUserId,
    name: polishNames[service.name],
    description: polishDescriptions[service.description]
})));
db.user_settings.insertOne({
    _id: polishUserId,
    currency: "PLN",
    footerText: "Przykładowa wycena prac ogrodniczych.",
    createdAt: new Date(),
    updatedAt: new Date()
});
db.calculations.insertMany(calculations.map((calculation, index) => ({
    ...calculation,
    _id: UUID(`61000000-0000-0000-0000-${String(index + 1).padStart(12, '0')}`),
    userId: polishUserId,
    currency: "PLN",
    name: polishNames[calculation.name],
    description: polishDescriptions[calculation.description],
    stages: calculation.stages.map(stage => ({
        ...stage,
        name: polishNames[stage.name],
        services: stage.services.map(item => ({ ...item, name: polishNames[item.name] })),
        materials: stage.materials.map(item => ({ ...item, name: polishNames[item.name] }))
    }))
})));
