<?php

namespace App;

use Firebase\JWT\JWT;
use Firebase\JWT\Key;

class Auth
{
    private $db;
    private $encryptor;
    
    public function __construct($db, $encryptor)
    {
        $this->db = $db;
        $this->encryptor = $encryptor;
    }
    
    /**
     * Verify Google OAuth Token
     */
    public function verifyGoogleToken($token)
    {
        try {
            $response = file_get_contents(
                'https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=' . urlencode($token)
            );
            
            $data = json_decode($response, true);
            
            if ($data['aud'] !== GOOGLE_CLIENT_ID) {
                throw new \Exception('Invalid client ID');
            }
            
            if ($data['exp'] < time()) {
                throw new \Exception('Token expired');
            }
            
            return $data;
            
        } catch (\Exception $e) {
            throw new \Exception('Google token verification failed: ' . $e->getMessage());
        }
    }
    
    /**
     * Create JWT Token
     */
    public function createJWT($userId, $email)
    {
        $issuedAt = new \DateTimeImmutable();
        $expire = $issuedAt->modify('+' . JWT_EXPIRY . ' seconds');
        
        $payload = [
            'iat' => $issuedAt->getTimestamp(),
            'exp' => $expire->getTimestamp(),
            'user_id' => $userId,
            'email' => $email
        ];
        
        return JWT::encode($payload, JWT_SECRET, 'HS256');
    }
    
    /**
     * Verify JWT Token
     */
    public function verifyJWT($token)
    {
        try {
            $decoded = JWT::decode($token, new Key(JWT_SECRET, 'HS256'));
            return (array) $decoded;
        } catch (\Exception $e) {
            throw new \Exception('JWT verification failed: ' . $e->getMessage());
        }
    }
    
    /**
     * Register or Login User
     */
    public function authenticateUser($googleData)
    {
        try {
            $email = $googleData['email'];
            $name = $googleData['name'] ?? 'User';
            $picture = $googleData['picture'] ?? null;
            
            // Check if user exists
            $user = $this->db->query(
                'SELECT * FROM users WHERE email = ?',
                [$email]
            )->fetch();
            
            if (!$user) {
                // Create new user
                $this->db->execute(
                    'INSERT INTO users (email, name, picture, created_at) VALUES (?, ?, ?, NOW())',
                    [$email, $name, $picture]
                );
                $userId = $this->db->lastInsertId();
            } else {
                $userId = $user['id'];
            }
            
            // Generate JWT
            $token = $this->createJWT($userId, $email);
            
            return [
                'success' => true,
                'token' => $token,
                'user_id' => $userId,
                'email' => $email
            ];
            
        } catch (\Exception $e) {
            return [
                'success' => false,
                'error' => $e->getMessage()
            ];
        }
    }
    
    /**
     * Get Current User from Token
     */
    public function getCurrentUser($token)
    {
        try {
            $payload = $this->verifyJWT($token);
            
            $user = $this->db->query(
                'SELECT id, email, name, picture FROM users WHERE id = ?',
                [$payload['user_id']]
            )->fetch();
            
            return $user;
            
        } catch (\Exception $e) {
            return null;
        }
    }
}
