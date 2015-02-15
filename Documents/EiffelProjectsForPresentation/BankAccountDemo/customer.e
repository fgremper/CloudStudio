note
	description: "A customer of a the bank."

class
	CUSTOMER

feature -- Customer properties

    name: STRING
			-- last name of the customer

    first_name: STRING
	 		-- the first name of the customer

    age: INTEGER
	 		-- age of the customer

feature -- Customer operations

	set_name(a_name: STRING)
			-- sets the name of the customer to 'a_name'
		do
			name := a_name
		end

	set_first_name(a_first_name: STRING)
			-- sets the first name of the customer to 'a_first_name'
		do
			first_name := a_first_name
		end

	set_age(a_age: INTEGER)
			-- sets the age of the customer to 'a_age'
		do
			age := a_age
		end

end
