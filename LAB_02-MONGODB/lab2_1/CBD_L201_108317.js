show dbs    
use miragaiadb
show collections    
db.users.insert({ "name": "David", "age": 28 })
db.users.find({ "_id": 1, "name": "Alice", "age": 31})
db.users.update({ "name": "Alice" }, { $set: { "age": 32} })
db.users.deleteMany({ "name": "David" })
db.users.getIndexes()