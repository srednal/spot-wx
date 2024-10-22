#!/bin/bash 

#Get highs and lows for today & tomorrow

# default to home, 2 days (today, tomorrow)
lat=${1:-"40.34942"}
lon=${2:-"-105.51366"}
days=${3:-2}

curl -s \
		--url-query latitude=${lat}								\
		--url-query longitude=${lon}							\
		--url-query forecast_days=${days}						\
		--url-query temperature_unit=fahrenheit					\
		--url-query lwind_speed_unit=mph						\
		--url-query lprecipitation_unit=inch					\
		--url-query timezone=America/Denver						\
		--url-query daily=weather_code							\
		--url-query daily=temperature_2m_max					\
		--url-query daily=temperature_2m_min,precipitation_sum	\
		--url-query daily=precipitation_probability_min			\
		--url-query daily=precipitation_probability_max			\
		--url-query daily=wind_direction_10m_dominant			\
		--url-query daily=wind_speed_10m_max					\
	'https://api.open-meteo.com/v1/forecast'					\
| $(dirname $0)/weather.jq
