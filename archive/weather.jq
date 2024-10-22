#!/usr/bin/jq -r -c -f

# Parse output from api.open-meteo.com into brief report, as in
# 10/19: Fog | 59/31°F | 0" (0-5%) | E@10mph
# 10/20: Overcast | 64/37°F | 0" (0-5%) | S@14mph


# Note - the longest output should be under 140 chars for txt
# This is 130 (when query is for 2 days)
# 10/19: Hvy Snow Shwrs | 100/-99°F | 99.9" (100-100%) | SE@125mph
# 10/20: Hvy Snow Shwrs |  100/-99°F | 99.9" (100-100%) | NW@125mph

# Might have to un-quarentine this (?)
# sudo xattr -d com.apple.quarantine weather.jq

# daily results
.daily |

# Wind direction from deg/45
[ "N", "NE", "E", "SE", "S", "SW", "W", "NW" ] as $windDir

|

# Weather condition codes, per open-meteo docs (abbreviated for shorter output)
[	"0",  
	"Clear", # 1
	"Pt Cldy", # 2
	"Overcast", # 3
	"4", "5", "6", "7", "8", "9", 
	"10", "11", "12", "13", "14", "15", "16", "17", "18", "19", 
	"20", "21", "22", "23", "24", "25", "26", "27", "28", "29", 
	"30", "31", "32", "33", "34", "35", "36", "37", "38", "39", 
	"40", "41", "42", "43", "44", 
	"Fog", # 45
	"46", "47",
	"Fog", # 48
	"49", "50",
	"Lt Drzle", # 51
	"52",
	"Mod Drzle", # 53
	"54", 
	"Hvy Drzle", # 55
	"Lt Frz Drzle", # 56
	"Hvy Frz Drzle", # 57
	"58", "59",	"60", 
	"Lt Rain", # 61
	"62", 
	"Mod Rain", # 63
	"64",
	"Hvy Rain", # 65
	"Lt Frz Rain", # 66
	"Hvy Frz Rain", # 67
	"68", "69", "70",
	"Lt Snow", # 71
	"72",
	"Mod Snow", # 73
	"74", 
	"Hvy Snow", # 75
	"76",
	"Snow Grains", # 77
	"78", "79",
	"Lt Showers", # 80
	"Mod Showers", # 81
	"Hvy Showers", # 82
	"83", "84", 
	"Lt Snow Shwrs", # 85
	"Hvy Snow Shwrs", # 86
	"87", "88", "89", "90", "91", "92", "93", "94", 
	"T-storm", # 95
	"T-storm", # 96
	"97", "98", 
	"T-storm" # 99
] as $wxCode

|

# collect info and format it
{
 "dy": .time | map( . | strptime("%Y-%m-%d") | mktime | strftime("%m/%d") ),	# strip off year
 "wx": .weather_code | map( $wxCode[.] ) ,										# translate weather code 
 "ht": .temperature_2m_max | map( . | round | tostring ),						# round temp
 "lt": .temperature_2m_min | map( . | round | tostring ),						# round temp
 "in": .precipitation_sum | map( . * 10 | round | . / 10 | tostring ),			# round precip to .1"
 "pl": .precipitation_probability_min | map( tostring ),
 "pu": .precipitation_probability_max | map( tostring ),
 "wd": .wind_direction_10m_dominant | map( . / 45 | round | $windDir[.] ),		# round wind to 45 deg, convert to direction
 "ws": .wind_speed_10m_max | map( . | round | tostring )						# round wind speed
} 

|

(
 . as $i 
 |
  # for each day
  .dy | keys | foreach .[] as $j ( null;

   # format a string
   "\($i.dy[$j]): \($i.wx[$j]) | \($i.ht[$j])/\($i.lt[$j])°F | \($i.in[$j])\" (\($i.pl[$j])-\($i.pu[$j])%) | \($i.wd[$j])@\($i.ws[$j])mph"
 )
)