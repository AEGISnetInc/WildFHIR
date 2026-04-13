USE wildfhirr4;
DELIMITER $$

CREATE FUNCTION calcDistanceKm(lat float, lng float, pnt_lat float, pnt_lng float)

RETURNS double
DETERMINISTIC
BEGIN

  RETURN 6371 * acos ( cos ( radians(pnt_lat) ) * cos( radians( lat ) ) * cos( radians( lng ) - radians(pnt_lng) ) + sin ( radians(pnt_lat) ) * sin( radians( lat ) ));

END$$

DELIMITER ;
