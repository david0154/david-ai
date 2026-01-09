<?php

/**
 * DAVID AI Backend - Entry Point
 * 
 * @author David Powered by Nexuzy Tech
 * @version 2.0.0
 */

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
header('Content-Type: application/json; charset=utf-8');

// Prevent direct access
if (php_sapi_name() === 'cli-server') {
    if (preg_match('/\.(?:js|css|png|jpg|jpeg|gif|ico|woff|woff2|ttf|svg)$/', $_SERVER['REQUEST_URI'])) {
        return false;
    }
}

// Load environment configuration
require_once __DIR__ . '/../config/bootstrap.php';

try {
    // Initialize application
    $app = new \App\Application();
    
    // Route the request
    $response = $app->handleRequest($_SERVER['REQUEST_METHOD'], $_SERVER['REQUEST_URI']);
    
    // Send response
    http_response_code($response['status'] ?? 200);
    echo json_encode($response);
    
} catch (\Exception $e) {
    // Error handling
    http_response_code(500);
    echo json_encode([
        'status' => 500,
        'error' => 'Internal Server Error',
        'message' => APP_DEBUG ? $e->getMessage() : 'An error occurred'
    ]);
}
