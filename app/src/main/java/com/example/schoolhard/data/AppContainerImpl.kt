package com.example.schoolhard.data

import android.content.Context

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
}

/**
 * Implementation for the Dependency Injection container at the application level.
 *
 * Variables are initialized lazily and the same instance is shared across the whole app.
 */