db.createUser(
        {
            user: "mongoadmin",
            pwd: "mongopass",
            roles: [
                {
                    role: "readWrite",
                    db: "microstreaming-analytics-db"
                }
            ]
        }
);
