<?php

namespace App;

class Sync
{
    private $db;
    private $encryptor;
    private $auth;
    
    public function __construct($db, $encryptor, $auth)
    {
        $this->db = $db;
        $this->encryptor = $encryptor;
        $this->auth = $auth;
    }
    
    /**
     * Sync User Data
     */
    public function syncData($userId, $dataType, $encryptedData, $timestamp)
    {
        try {
            // Verify timestamp is recent (within 5 minutes)
            if (abs(time() - $timestamp) > 300) {
                throw new \Exception('Timestamp too old or in future');
            }
            
            // Check for existing sync record
            $existing = $this->db->query(
                'SELECT * FROM sync_data WHERE user_id = ? AND data_type = ?',
                [$userId, $dataType]
            )->fetch();
            
            if ($existing) {
                // Update existing
                $this->db->execute(
                    'UPDATE sync_data SET data = ?, timestamp = NOW() WHERE id = ?',
                    [$encryptedData, $existing['id']]
                );
            } else {
                // Insert new
                $this->db->execute(
                    'INSERT INTO sync_data (user_id, data_type, data, timestamp) VALUES (?, ?, ?, NOW())',
                    [$userId, $dataType, $encryptedData]
                );
            }
            
            return ['success' => true, 'message' => 'Data synced successfully'];
            
        } catch (\Exception $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }
    
    /**
     * Get Synced Data
     */
    public function getData($userId, $dataType = null)
    {
        try {
            $query = 'SELECT * FROM sync_data WHERE user_id = ?';
            $params = [$userId];
            
            if ($dataType) {
                $query .= ' AND data_type = ?';
                $params[] = $dataType;
            }
            
            $results = $this->db->query($query, $params)->fetchAll();
            
            return ['success' => true, 'data' => $results];
            
        } catch (\Exception $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }
    
    /**
     * Resolve Sync Conflicts
     */
    public function resolveConflict($userId, $dataType, $localData, $localTimestamp, $remoteTimestamp)
    {
        try {
            // Keep the most recent version
            if ($localTimestamp > $remoteTimestamp) {
                // Keep local
                $winner = 'local';
                $data = $localData;
            } else {
                // Use remote
                $winner = 'remote';
                $data = $this->getData($userId, $dataType);
            }
            
            return [
                'success' => true,
                'winner' => $winner,
                'data' => $data
            ];
            
        } catch (\Exception $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }
}
