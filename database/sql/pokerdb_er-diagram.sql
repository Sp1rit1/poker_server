CREATE TABLE IF NOT EXISTS "users" (
	"id" serial NOT NULL UNIQUE,
	"username" varchar(255) NOT NULL UNIQUE,
	"password_hash" varchar(255) NOT NULL,
	"email" varchar(255) NOT NULL UNIQUE,
	"created_at" timestamp with time zone NOT NULL,
	"friend_code" varchar(6) NOT NULL UNIQUE,
	"balance" numeric(10,2) NOT NULL DEFAULT '1000',
	PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "user_friends" (
	"user_id1" bigint NOT NULL,
	"user_id2" bigint NOT NULL,
	"status" varchar(255) NOT NULL DEFAULT 'accepted',
	"created_at" timestamp with time zone DEFAULT 'current_timestamp',
	PRIMARY KEY ("user_id1", "user_id2")
);

CREATE TABLE IF NOT EXISTS "user_stats" (
	"user_id" bigint NOT NULL,
	"hands_played" bigint NOT NULL DEFAULT '0',
	"hands_won" bigint NOT NULL DEFAULT '0',
	"total_winnings" bigint NOT NULL DEFAULT '0',
	"last_updated" timestamp with time zone NOT NULL DEFAULT 'current_timestamp',
	PRIMARY KEY ("user_id")
);


ALTER TABLE "user_friends" ADD CONSTRAINT "user_friends_fk0" FOREIGN KEY ("user_id1") REFERENCES "users"("id");

ALTER TABLE "user_friends" ADD CONSTRAINT "user_friends_fk1" FOREIGN KEY ("user_id2") REFERENCES "users"("id");
ALTER TABLE "user_stats" ADD CONSTRAINT "user_stats_fk0" FOREIGN KEY ("user_id") REFERENCES "users"("id");