package com.example.getcurrentlocatonexample.models

class MyLocation {
    var name: String? = null
    var lat: Double? = null
    var long: Double? = null

    constructor()
    constructor(name: String?, lat: Double?, long: Double?) {
        this.name = name
        this.lat = lat
        this.long = long
    }


}