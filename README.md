# Pet Store 

## Author
Matthew Ilichov   
matvey.ilyichov@gmail.com
https://www.linkedin.com/in/matthew-ilichov-28720123/

## Requirements
java 1.8+   
stb 1.0.4+   
scala 2.12.8+

## Configs and DB
Config for DB is located at src/main/resources/application.conf.   
DB is located at src/main/resources/sqlite.db by default config. It is possible to change path to DB by updating "sqllite.driver.db.url" key at config file correspondingly. 

## Compile and run
```
sbt clean "runMain ua.mackenzy.api.PetStoreApp"
```
## API

### Add a new pet   
**request:**
```
curl -X POST \
  http://localhost:8080/pet \
  -H 'Content-Type: application/json' \
  -d '{
    "category": {
        "id": 1,
        "name": "cat"
    },
    "name": "Meow",
    "photoUrl": "http://meow.com"
}'
```
**response:**
```
{
  "id": 5
}
```

### Get pet by id   
**request:**
```
curl -X GET http://localhost:8080/pet/5
```
**response:**
```
{
    "id": 5,
    "category": {
        "id": 1,
        "name": "cat"
    },
    "name": "Meow",
    "photoUrl": "http://meow.com"
}
```


### Get all pets   
**request:**
```
curl -X GET http://localhost:8080/pets
```
**response:**
```
[
    {
        "id": 1,
        "category": {
            "id": 1,
            "name": "cat"
        },
        "name": "Doggie",
        "photoUrl": "https://www.what-dog.net/Images/faces2/scroll0013.jpg"
    },
    {
        "id": 2,
        "category": {
            "id": 1,
            "name": "cat"
        },
        "name": "Doggo"
    },
    {
        "id": 3,
        "category": {
            "id": 2,
            "name": "dog"
        },
        "name": "Kittie",
        "photoUrl": "https://boygeniusreport.files.wordpress.com/2017/01/cat.jpg"
    },
    {
        "id": 4,
        "category": {
            "id": 2,
            "name": "dog"
        },
        "name": "Cat"
    },
    {
        "id": 5,
        "category": {
            "id": 1,
            "name": "cat"
        },
        "name": "Meow",
        "photoUrl": "http://meow.com"
    }
]
```

### Place an order   
**request:**
```
curl -X POST \
     http://localhost:8080/store/order \
     -H 'Content-Type: application/json' \
     -d '{
       "petId": 5,
       "userId": 2
   }'
```
**response:**
```
{
  "id": 1
}
```

### Get order info by Order ID   
**request:**
```
curl -X GET http://localhost:8080/store/order/1
```
**response:**
```
{
    "pet": {
        "category": {
            "id": 1,
            "name": "cat"
        },
        "name": "Meow",
        "photoUrl": "http://meow.com"
    },
    "user": {
        "id": 2,
        "name": "Martin Odersky"
    },
    "status": {
        "id": 1,
        "value": "placed"
    }
}
```

### Update status of an order   
**request:**
```
curl -X PUT \
  http://localhost:8080/store/order/1/status \
  -H 'Content-Type: application/json' \
  -d '{
    "id": 2
}'
```

### Create new user   
**request:**
```
curl -X POST \
  http://localhost:8080/user \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "John Cena"
}'
```
**response:**
```
{
   "id": 4
}
```
### Get user by ID   
**request:**
```
curl -X GET http://localhost:8080/user/4 
```
**response:**
```
{
    "id": 4,
    "name": "John Cena"
}
```
### Get all users   
**request:**

```
curl -X GET http://localhost:8080/users 
```
**response:**
```
[
    {
        "id": 1,
        "name": "John A De Goes"
    },
    {
        "id": 2,
        "name": "Martin Odersky"
    },
    {
        "id": 3,
        "name": "Phil Swift"
    },
    {
        "id": 4,
        "name": "John Cena"
    }
]
```
