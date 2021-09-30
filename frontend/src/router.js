
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import WarehouseManager from "./components/WarehouseManager"

import OrderManager from "./components/OrderManager"

import DeliveryManager from "./components/DeliveryManager"


import MyPage from "./components/MyPage"
export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/warehouses',
                name: 'WarehouseManager',
                component: WarehouseManager
            },

            {
                path: '/orders',
                name: 'OrderManager',
                component: OrderManager
            },

            {
                path: '/deliveries',
                name: 'DeliveryManager',
                component: DeliveryManager
            },


            {
                path: '/myPages',
                name: 'MyPage',
                component: MyPage
            },


    ]
})
