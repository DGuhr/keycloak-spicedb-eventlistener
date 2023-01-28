# Convince ChatGPT to do bash scripting for me

## The problem
I freely admit it: **I don't like bash scripting.** Yes, it's out, chat scripting is my nemesis, i find these scripts are verbose and ugly for me, no types, just... go away. 

Don't get me wrong, I usualy _**can**_ script in bash or any shell, but not without constant googling, headaches and urges to throw my laptop out of the window.

So... long story short, i thought, why not let ChatGPT, the newest hype in town, do that for me? 
Well, here's how it went:

## Prompts:
I created a very quick example from the existing [initialize-poc.sh](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/77875514d23956e5b689a4da87d4c4c8b438194f/initialize-poc.sh#L45-L46) at that state - initially i wanted ChatGPT to "just" generate example commands, because I am lazy. Here's the prompt I initially used:
```
chatgpt data generator prompt kcadm:

I want you to create randomized test data for kcadm.sh, an administration CLI tool for an IAM open source software named keycloak.

To add a user to keycloak using kcadm.sh, the following prompt is needed:
/opt/keycloak/bin/kcadm.sh create users -r <realm> -s username=<name> -s firstName=<firstname> -s lastName=<lastname> -s enabled=true -s email=<email>  -s emailVerified=true -s "attributes.org_id=<orgid>"

an example is
/opt/keycloak/bin/kcadm.sh create users -r master -s username=paula -s firstName=Paula -s lastName=Von -s enabled=true -s email=paula@demo.com  -s emailVerified=true -s "attributes.org_id=12345"

after these users are created, a password is set using the following prompt for every user:
/opt/keycloak/bin/kcadm.sh set-password -r <realm> --username <username> --new-password <newpassword>

so the full example would be 

/opt/keycloak/bin/kcadm.sh create users -r master -s username=paula -s firstName=Paula -s lastName=Von -s enabled=true -s email=paula@demo.com  -s emailVerified=true -s "attributes.org_id=12345"
/opt/keycloak/bin/kcadm.sh set-password -r master --username paula --new-password demo1234!


Create example kcadm commands for creating users and password for 10 users for me with random values for email, firstName, username, lastName, attributes.org_id and new-password 
```

![prompt1 and answer_part_1](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/chatgpt_generator/assets/answer1_p1.png?raw=true)

That answer contained only 7 answers, so... i "decently" said: hey ChatGPT, y u no 10?!

```
these ar eonly seven

```
Also it was saturday morning, so i couldn't even type right. Well, things happen. Luckily ChatGPT had enough to do with us dumb humans so far, so it understood me and created the other 3 examples:
![answer1_part2_and question2_answer2_part1](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/chatgpt_generator/assets/answer1_p2_and_q2_a2_p1.png?raw=true)

Ok... I was interested, to be fair. Now it's still saturday, and I wanted to go bouldering and later there's this poetry slam, and generating example prompts took time, and so... could we make this even more generic? I asked simply:
```
can you create a script that allows to add as much users as i want?
```
![answer2_part1_and_question3](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/chatgpt_generator/assets/answer2_p2_question3.png?raw=true)

and the answer was: yes. yes, sure! And I thought: Good machine!
![question3_p1](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/chatgpt_generator/assets/answer3_p1.png?raw=true)

And it even explained me the script:
![question3_p2_command4](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/chatgpt_generator/assets/answer3_p2_command4.png?raw=true)
