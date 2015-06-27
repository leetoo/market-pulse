var marketPulseApp = angular.module('marketPulseApp', ['ngRoute', 'marketPulseControllers', 'marketPulseServices']);

marketPulseApp.config(['$routeProvider', function($routeProvider) {
    $routeProvider
        .when('/exchange-stats', {
            templateUrl: '/assets/javascripts/partials/exchange-stats.html',
            controller: 'ExchangeStatsCtrl'
        }).when('/trade-publisher', {
            templateUrl: '/assets/javascripts/partials/trade-publisher.html',
            controller: 'TradePublisherCtrl'
        }).otherwise({
            redirectTo: '/exchange-stats'
        });
}]);