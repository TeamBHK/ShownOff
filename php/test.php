<?php

include './libs/content_provider.php';

$str = '[{"trash":[],"unsynced":[{"id":3,"totalCost":2000,"isCovered":true,"unitCost":2000,"name":"Fruits","last_mod":100000000,"mukolo_id":21,"quantity":1,"s_id":0,"user_id":1,"units":""}],"table":"budget","updated":[],"last_id":0,"localData":[]}]';

$syncData = json_decode(stripslashes($str));

sync($syncData);

function sync($syncData) {

    $provider = new contentProvider(getConnection());
    $syncResults = array();

    try {
        foreach ($syncData as $data) {
            array_push($syncResults, $provider->sync($data));
        }
        responde($syncResults, "1", $provider->message);
    } catch (Exception $exc) {

        responde($syncResults, "0", $exc->getTraceAsString());
    }
}

function getConnection() {
    $dbhost = "127.0.0.1";
    $dbuser = "root";
    $dbpass = "";
    $dbname = "planb";
    $dbh = new PDO("mysql:host=$dbhost;dbname=$dbname", $dbuser, $dbpass);
    $dbh->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    return $dbh;
}


function responde($results, $status = "1", $message = "ok") {
    $response["data"] = $results;
    $response["success"] = $status;
    $response["message"] = $message;
    echo json_encode($response);
}