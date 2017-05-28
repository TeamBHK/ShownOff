<?php
include './includes/content_provider.php';
$json = file_get_contents('php://input');
$syncData = json_decode($json);

sync($syncData);

function sync($syncData) {
       $provider = new ContentProvider(getConnection());
       $syncResults = array();
    try {
            foreach ($syncData as $data) {
            array_push($syncResults, $provider->sync($data));
        }
        responde($syncResults, "1", $provider->message);
    } catch (Exception $exc) {
        responde($syncResults, "0", $exc->getMessage().$provider->message);
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
