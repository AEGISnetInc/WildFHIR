USE wildfhirr4
DELIMITER $$

create function calcDistanceMi(lat float, lng float, pnt_lat float, pnt_lng float)

Returns double DETERMINISTIC
BEGIN

Declare dist double;
SET dist =
  3959 * acos (
  cos ( radians(pnt_lat) )
  * cos( radians( lat ) )
  * cos( radians( lng ) - radians(pnt_lng) )
  + sin ( radians(pnt_lat) )
  * sin( radians( lat ) )
);

RETURN dist;

END
