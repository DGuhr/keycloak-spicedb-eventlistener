# Convince ChatGPT to do bash scripting for me

## The problem
I freely admit it: **I don't like bash scripting.** Yes, it's out, bash scripting is my nemesis, i find these scripts are verbose and ugly for me, no types, just... go away. 

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

![prompt1 and answer_part_1](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/main/assets/answer1_p1.png?raw=true)

That answer contained only 7 answers, so... i "decently" said: hey ChatGPT, y u no 10?!

```
these ar eonly seven

```
Also it was saturday morning, so i couldn't even type right. Well, things happen. Luckily ChatGPT had enough to do with us dumb humans so far, so it understood me and created the other 3 examples:
![answer1_part2_and question2_answer2_part1](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/main/assets/answer1_p2_and_q2_a2_p1.png?raw=true)

Ok... I was interested, to be fair. Now it's still saturday, and I wanted to go bouldering and later there's this poetry slam, and generating example prompts took time, and so... could we make this even more generic? I asked simply:
```
can you create a script that allows to add as much users as i want?
```
![answer2_part1_and_question3](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/main/assets/answer2_p2_question3.png?raw=true)

and the answer was: yes. yes, sure! And I thought: Good machine!
![question3_p1](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/main/assets/answer3_p1.png?raw=true)

And it even explained me the script:
![question3_p2_command4](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/main/assets/answer3_p2_command4.png?raw=true)

but hey, totally random org_ids is a bit much, so boil them down to three. and also i want to call this script in a bigger context, so ChatGPT, make it parametrized:
``` 
make the script so that the org_id will only contain 3 distinct ids. Also, make the script so that it takes an argument for $num_users
```

and ChatGPTs answer was: yeh, no problem!
![answer4_p1](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/main/assets/answer4_p1.png?raw=true)
![answer4_p2](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/main/assets/answer4_p2.png?raw=true)

Looking at the ChatGPT generated script, i didn't like the naming "org1", "org2", "org3" so I wanted it to create numerical values instead:
``` 
change the script so that the org_id values are numeric values with 12 digits 
```
And it generated me the following script:
![command_5a](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/main/assets/command_5a.png?raw=true)
As you can see, I stopped the generation because I immediately saw ChatGPT followed my words too literally, because the intent was to still have only 3 distinct values. Well, no machine's perfect, so let's try again - but now also use only 8 digits, bc 12 may be a bit much (and randomly chosen, so who cares):
``` 
change the script so that the org_id values are numeric values with 8 digits, but there would still be only 3 distinct values for all generated users
```
and here's the outcome:
![command_5_and_answer5_part1](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/main/assets/command_5_and_a5_p1.png?raw=true)
and the explanation:
![answer5_part2](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/main/assets/answer5_p2.png?raw=true)

So, I had to try this... simply removed my image using `docker compose down -v` followed by `docker rmi dguhr/keycloak_spicedbtest` and then `docker compose up` afterwards: It worked. exactly as intended. There was only one problem left: As per [the custom entrypoint script at that point in time](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/77875514d23956e5b689a4da87d4c4c8b438194f/initialize-poc.sh#L63), I use the usernames of the generated users in a later step to get their IDs when created, to add them to groups afterwards by their ID.

Well, I don't have access to the generated usernames. Easy, I know, but now I wanted ChatGPT to go all the way, so I asked:
```
can you change it so that the username for each user is saved for subsequent calls in a variable? Example: After running the script, i want to get the value of the username for user1 by calling $username1 in another call
```

and here's its answer:
![answer6](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/main/assets/answer6.png?raw=true)

So, what should I say? As you can [see in the script](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/d19e1510faded97ff7ce46efbbd247cbe6fcfe17/initialize-poc.sh#L43-L44) when running this funny side project, the ChatGPT-created [usergenerator.sh](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/d19e1510faded97ff7ce46efbbd247cbe6fcfe17/usergenerator.sh) works like a charm. Timewise, this took around an hour to "create" and test, and another 45 min to write this blog post. 
It may not have taken longer building it all on my own, but I would definitely not write this blog post then and be in a good mood.

So... I am not saying it is perfect (the script doesn't even run on my machine because `tr -dc...` on MacOS behaves differently than on linux and the script creates an `error: illegal byte sequence` instead, but hey, we're running in docker on linux, and it's still weekend) - but it was fun to do. 

I actually played around quite a bit with feeding ChatGPT with the schema reference of authzed spiceDB and the results were also quite likely, at least for simple schemas. But that's for another day. Hope you had fun reading this. Have a great weekend!

Off bouldering,
Dom

# Update 1:
Oh, dare you, bash! For sure the variables defined in the script are not made available to the calling script unless you use source! But luckily ChatGPT is a bash magician, so after a little fiddling - or lets say, crying for help... 
``` 
i have the following lines in my calling script that calls the script1.sh beforehand: 
echo "Username 1 is set to ${username1}"
user1Uid=$(/opt/keycloak/bin//kcadm.sh get users -r master -q username=$username1 --fields=id | awk -F':' '{print $2}' | grep . | tr -d "\"" | sed -e 's/^[[:space:]]*//')
The ${username1} echo returns an empty result. help
```
it gave me the actual correct response: use `source` you brat!
![q_and_a7](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/main/assets/q_and_a7.png?raw=true)

So now the [script](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/aa6ace86bef29a483359691bc6e2eb8954b8c824/usergenerator.sh) and [calling it the working way](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/aa6ace86bef29a483359691bc6e2eb8954b8c824/initialize-poc.sh#L43-L44) and [getting the variable for making the group member adding call](https://github.com/DGuhr/keycloak-spicedb-eventlistener/blob/aa6ace86bef29a483359691bc6e2eb8954b8c824/initialize-poc.sh#L57-L58) really works :) 