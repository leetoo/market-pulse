var marketPulseServices = angular.module('marketPulseServices', ['ngResource']);

marketPulseServices.factory('Trade', ['$resource',
    function($resource) {
        return $resource('/api/trades', {}, {
            create: {method:'POST', params:{}}
        });
    }])