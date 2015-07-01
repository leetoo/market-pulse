var marketPulseControllers = angular.module('marketPulseControllers', []);

marketPulseControllers.controller('ExchangeStatsCtrl', ['$scope', 'ExchangeStats', function($scope, ExchangeStats) {
    $scope.collection = ExchangeStats.collection;
}]);


marketPulseControllers.controller('TradePublisherCtrl', ['$scope', 'Trade', function($scope, Trade) {
    $scope.trade = {
      userId: "123456",
      currencyFrom: "EUR",
      currencyTo: "GBP",
      amountSell: 1000.0,
      amountBuy: 710.0,
      rate: 0.71,
      timePlaced: "27-JUN-15 18:15:10",
      originatingCountry: "UK"
    };

    $scope.onRateChange = function(r) {
        $scope.trade.amountBuy = Math.round($scope.trade.amountSell * r * 100) / 100;
    }

    $scope.onAmountSellChange = function(a) {
        $scope.trade.amountBuy = Math.round(a * $scope.trade.rate * 100) / 100;
    }

    $scope.publishTrade = function() {
        Trade.create($scope.trade);
    };
}]);

