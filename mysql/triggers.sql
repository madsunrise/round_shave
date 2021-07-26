use round_shave;

DELIMITER //

CREATE TRIGGER appointment_time_validation
BEFORE INSERT
   ON appointment FOR EACH ROW
BEGIN

IF exists (select 1 from appointment where (user_id = NEW.user_id) AND
    (NEW.start_time = start_time OR (NEW.start_time < start_time AND
    NEW.end_time > start_time) OR (start_time < NEW.start_time AND
     end_time > NEW.start_time))) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'this time is already taken';
END IF;

END; //

DELIMITER ;
