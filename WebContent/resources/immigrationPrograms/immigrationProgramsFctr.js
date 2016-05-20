angular.module("commonHttpRequests")
    .factory("getCategories", ["URLS", "$http", function (URLS, $http) {
        return function (countrySelected) {
            return $http.post(URLS.URL + ":" + URLS.PORT + URLS.ROOT_CONTEXT + URLS.REQUEST_MAPPING
                        + URLS.CATEGORIES_OF_PROGRAM_BY_COUNTRY, countrySelected)
                .then(function (response) {
                    return {
                        categories: response.data
                    }
                })
        }
    }])
    .factory("getPrograms", ["URLS", "$http", function (URLS, $http) {
        return function (countrySelected_categorySelected) {
            return $http.post(URLS.URL + ":" + URLS.PORT + URLS.ROOT_CONTEXT + URLS.REQUEST_MAPPING
                    + URLS.IMIGRATION_PROGRAMS, countrySelected_categorySelected)
                .then(function (response) {
                    return {
                        programs: response.data
                    }
                })
        }
    }]);