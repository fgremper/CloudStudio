note
    description: "Root class of the bank account demo."

class
    MAIN

inherit
    ARGUMENTS

create
    make

feature {NONE} -- Initialization

    make
            -- Run the bank application.
        local
            owner: CUSTOMER
            account: SAVINGS_ACCOUNT
        do
            print ("### Welcome to the bank! ###%N")

            create owner.make ("Doe", "John", 42)
            create account.make_with_balance_and_owner (100, owner)

            account.deposit (100)
            account.withdraw (50)
            account.deposit (200)
            account.withdraw (40)
            account.pay_out_interest

            account.print_log
        end

end
