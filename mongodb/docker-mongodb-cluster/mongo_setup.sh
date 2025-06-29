#!/bin/sh
sleep 10
echo "Initiating Replica Set..."
mongosh --host mongo1:27017 -u root -p rootpass <<EOF
rs.initiate({
  _id: "rs1",
  members: [
    { _id: 0, host: "mongo1:27017" },
    { _id: 1, host: "mongo2:27018" },
    { _id: 2, host: "mongo3:27019" }
  ]
})

rs.status()

EOF