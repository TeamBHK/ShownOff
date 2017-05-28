<?php

/**
 * Description of ContentProvider
 *This class handles CRUD operations on the database.
 * @author katoj
 */

 class ContentProvider {

    public $message = "";
    private $_dbh;
    private $syncResults;

    function __construct($db) {
        $this->_dbh = $db;
    }

    public function insert($table, $values) {

        $values = (array) $values;
        $content_values = array();
        //Retriving the field names from array
        //NOTE: The key represents a field name 
        $projection = "(";
        $counter = 0;

        foreach ($values as $key => $value) {
            //Add values to array cantent_values
            array_push($content_values, $value);
            //IF its the end ot the array, put close brackets instead of comma
            if ($counter < count($values) - 1) {
                $projection.= $key . ", ";
            } else {
                $projection.= $key . ")";
            }
            $counter++;
        }

        //Adding question marks to the values brackets
        $value_holder = "VALUES (?";
        for ($i = 1; $i < count($values); $i++) {
            $value_holder.=",?";
        }

        $value_holder.=")";
        //Prepareing the PDO statement   
        $query = "INSERT INTO " . $table . " " . $projection . " " . $value_holder . " ";
        $stmt = $this->_dbh->prepare($query);

        try {
            $stmt->execute($content_values);
            return $this->_dbh->lastInsertId();
        } catch (Exception $ex) {
            $this->message.= $ex->getMessage();
            return 0;
        }
    }

    public function query($table, $projection, $selection, $selectionArgs, $sortOrder = "", $limit = "", $isArray = false) {
        $query = "SELECT " . $projection . " FROM " . $table;
        if (!empty($selection)) {
            $query.=" WHERE " . $selection;
        } else if (!empty($sortOrder)) {
            $query.=" ORDER BY " . $sortOrder;
        } else if (!empty($limit)) {
            $query.=" LIMIT " . $limit;
        }
        try {

            $stmt = $this->_dbh->prepare($query);
            $stmt->execute($selectionArgs);

            if ($stmt->rowCount() == 1 && !$isArray) {
                return (array) $stmt->fetchObject();
            } elseif ($stmt->rowCount() > 1 || $isArray) {
                return (array) $stmt->fetchAll(PDO::FETCH_OBJ);
            } else {
                return $stmt->rowCount();
            }
        } catch (Exception $exc) {
            $this->message.=$exc->getMessage();
            return 0;
        }
    }

    public function update($table, $selection, $selectionArgs, $values) {

        $values = (array) $values;
        $content_values = array();
        //Retriving the field names from array
        //NOTE: The key represents a field name 
        $projection = "";
        $c = 0;

        foreach ($values as $key => $value) {

            //Add values to array cantent_values
            array_push($content_values, $value);
            $projection.= $key;

            //IF its the end ot the array, put close brackets instead of comma
            if ($c < count($values) - 1) {
                $projection.= "=?, ";
            } else {
                $projection.= "=?";
            }

            $c++;
        }

        array_push($content_values, $selectionArgs[0]);
        $query = "UPDATE " . $table . " SET " . $projection . " WHERE " . $selection;
        //$this->message.=$query;

        try {
            $stmt = $this->_dbh->prepare($query);
            $stmt->execute($content_values);
            return $stmt->rowCount();
        } catch (Exception $exc) {
            $this->message.=$exc->getMessage();
            return 0;
        }
    }

    public function delete($table, $selection, $selectionArgs) {

        try {
            $stmt = $this->_dbh->prepare("DELETE FROM " . $table . " WHERE " . $selection . " ");
            $stmt->execute($selectionArgs);
            return $stmt->rowCount();
        } catch (Exception $exc) {
            
            $this->message.= $exc->getMessage();
            return 0;
        }
    }

    public function sync($syncData) {
        $table = $syncData->table;
        $this->trashOut($table, $syncData->trash);
        $this->getNew($table, $syncData->last_id);
        $this->syncNew($table, $syncData->newData);
        $this->syncUpdates($table, $syncData->updated);
        $this->syncDown($table, $syncData->localData);
        $this->syncResults['table'] = $table;
        return $this->syncResults;
    }

    private function trashOut($table, $d) {

        foreach ($d as $id) {

            if ($table == "photos") {
                $this->deletePhoto($table, $id);
            } else {
                $this->syncDelete($table, $id);
            }
        }
    }

    private function getNew($table, $lastId) {

        $this->syncResults["new"] = array();
        $selection = "id>?";
        $projection = "*";
        $selectionArgs[0] = $lastId;
        $n = $this->query($table, $projection, $selection, $selectionArgs, "", "", TRUE);

        if ($n) {
            $this->syncResults["new"] = $n;
        }
    }

    private function syncNew($table, $d) {

        $new = array();

        foreach ($d as $values) {

            $values = (array) $values;
            $n = array();
            $n["id"] = $values['id'];
            $last_mod = time() * 1000;

            $values["last_mod"] = $last_mod;
            $n['last_mod'] = $last_mod;

            unset($values['id']);
            unset($values['s_id']);

            $n["s_id"] = $this->insert($table, $values);
            $n['name']=$values['name'];

            array_push($new, $n);
        }

        $this->syncResults["sync"] = $new;
    }

    private function syncUpdates($table, $d) {

        $updates = array();
        foreach ($d as $values) {

            $u = array();
            $values = (array) $values;
            $selection = "id=?";
            $selectionArgs[0] = $values['s_id'];
            $lastMod = time() * 1000;
            $values["last_mod"] = $lastMod;

            $u['id'] = $values['id'];
            $u['name'] = $values['name'];
            $u['last_mod'] = $lastMod;

            unset($values['s_id']);
            unset($values['id']);

            if ($this->update($table, $selection, $selectionArgs, $values)) {
                array_push($updates, $u);
            }
        }

        $this->syncResults["updates"] = $updates;
    }

    private function syncDown($table, $d) {

        $modified = array();
        $trash = array();

        foreach ($d as $data) {

            $data = (array) $data;
            $selectionArgs[0] = $data['s_id'];
            $selection = "id=?";
            $projection = "*";

            $row = $this->query($table, $projection, $selection, $selectionArgs);

            if ($row) {
                //if the field hs been modified
                if ($row['last_mod'] > $data['last_mod']) {
                    //add the filed to the modified syncResults
                    //include the serve_id and the local id
                    $row['id'] = $data['id'];
                    $row['s_id'] = $data['s_id'];

                    array_push($modified, $row);
                }
            } else {
                array_push($trash, $data['id']);
            }
        }

        $this->syncResults["modified"] = $modified;
        $this->syncResults["trash"] = $trash;
    }

    private function deletePhoto($table, $id) {
        $selectionArgs[0] = $id;
        $projection = "photo,thumbnail";
        $selection = "id=?";
        $row = $this->query($table, $projection, $selection, $selectionArgs);
        $path = $row['photo'];
        $thumb = $row['thumbnail'];

        if (file_exists($path)) {
            unlink($path);
        }

        if (file_exists($thumb)) {
            unlink($thumb);
        }
        return $this->syncDelete($table, $id);
    }

    private function syncDelete($table, $id) {
        $selection = "id=?";
        $selectionArgs[0] = $id;
        return $this->delete($table, $selection, $selectionArgs);
    }

}
