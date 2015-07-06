var marketPulseControllers = angular.module('marketPulseControllers', []);

marketPulseControllers.controller('ExchangeStatsCtrl', ['$scope', 'ExchangeStats', function($scope, ExchangeStats) {
    $scope.collection = ExchangeStats.collection;
    $scope.size = ExchangeStats.size;
}]);


marketPulseControllers.controller('TradePublisherCtrl', ['$scope', 'Trade', function($scope, Trade) {
    var fixedRates = {
        'EUR': {
            'EUR': 1.0,
            'GBP': 0.71,
            'PLN': 4.15
        },
        'GBP': {
            'EUR': 1.41,
            'GBP': 1.0,
            'PLN': 5.92
        },
        'PLN': {
            'EUR': 0.24,
            'GBP': 0.17,
            'PLN': 1.0
        }
    };

    $scope.countries = ['UK', 'PL']
    $scope.currencies = Object.keys(fixedRates);

    $scope.trade = {
      userId: Math.floor(Math.random() * 1000000).toString(),
      currencyFrom: $scope.currencies[1],
      currencyTo: $scope.currencies[2],
      amountSell: 1000.0,
      amountBuy: 710.0,
      rate: 0.71,
      timePlaced: (new Date()).toString('dd-MMM-yyyy HH:mm:ss'),
      originatingCountry: $scope.countries[0]
    };

    $scope.$watch('trade.rate', function(newvalue, oldvalue) {
        $scope.trade.amountBuy = Math.round($scope.trade.amountSell * newvalue * 100) / 100;
    });

    $scope.$watch('trade.amountSell', function(newvalue, oldvalue) {
        $scope.trade.amountBuy = Math.round(newvalue * $scope.trade.rate * 100) / 100;
    });

    $scope.$watch('trade.currencyFrom', function(newvalue, oldvalue) {
        $scope.trade.rate = fixedRates[newvalue][$scope.trade.currencyTo];
    });

    $scope.$watch('trade.currencyTo', function(newvalue, oldvalue) {
        $scope.trade.rate = fixedRates[$scope.trade.currencyFrom][newvalue];
    });

    $scope.publishTrade = function() {
        Trade.create($scope.trade);
    };
}]);

