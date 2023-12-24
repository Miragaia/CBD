from os import write
import csv
from neo4j import GraphDatabase

class lab4_4:
    
    def __init__(self, uri, user, password):
        self.driver = GraphDatabase.driver(uri, auth=(user, password))
        
        self.questions = [
            "1. Top 5 scorers in Serie A league",
            "2. Player with the best relation Goals per match",
            "3. Player with the highest deviation between goals and expected goals (xG)d",
            "4. Total number of players in each league",
            "5. Top scorer in each league",
            "6. Player with highest xG in each league",
            "7. Top scorer in each country",
            "8. Total minutes played in each league",
            "9. Top 5 clubs with the highest number of goals scored",
            "10. Total number of players in each country",
            "11. Top 5 leagues with the highest xG"
        ]
        
        self.answers = ["""
                        MATCH (l:League {name: 'Serie A'})<-[:ACTED_IN]-(p:Player)-[:HAS_STATS]->(ps:PlayerStats)
                        WITH p, ps
                        ORDER BY ps.goals DESC
                        LIMIT 5
                        RETURN p.name AS TopScorer, ps.goals AS Goals;
                        """,
                        """
                        MATCH (p:Player)-[:HAS_STATS]->(ps:PlayerStats)
                        WITH p, ps
                        ORDER BY ps.goals / ps.matchesPlayed DESC
                        LIMIT 3
                        RETURN p.name AS BestScorer, ps.goals AS Goals, ps.matchesPlayed AS MatchesPlayed;
                        """,
                        """
                        MATCH (p:Player)-[:HAS_STATS]->(ps:PlayerStats)
                        WITH p, ps, (ps.goals - ps.xG) AS GoalDeviation
                        ORDER BY GoalDeviation ASC
                        LIMIT 1
                        RETURN p.name AS WorstScorer, ps.goals AS Goals, ps.xG AS ExpectedGoals, GoalDeviation AS GoalDeviation;
                        """,
                        """
                        MATCH (l:League)<-[:ACTED_IN]-(p:Player)
                        WITH l, p
                        RETURN l.name AS League, COUNT(DISTINCT p) AS NumberOfPlayers
                        ORDER BY League;
                        """,
                        """
                        MATCH (l:League)<-[:ACTED_IN]-(p:Player)-[:HAS_STATS]->(ps:PlayerStats)
                        WITH l, p, ps
                        ORDER BY ps.goals DESC
                        WITH l, COLLECT({player: p, stats: ps})[0] AS bestScorer
                        RETURN l.name AS League, bestScorer.player.name AS TopScorer, bestScorer.stats.goals AS Goals;
                        """,
                        """
                        MATCH (l:League)<-[:ACTED_IN]-(p:Player)-[:HAS_STATS]->(ps:PlayerStats)
                        WITH l, p, ps
                        ORDER BY ps.xG DESC
                        WITH l, COLLECT({player: p, stats: ps})[0] AS bestXGPlayer
                        RETURN l.name AS League, bestXGPlayer.player.name AS TopXGPlayer, bestXGPlayer.stats.xG AS ExpectedGoals;
                        """,
                        """
                        MATCH (c:Country)<-[:IN_COUNTRY]-(l:League)<-[:ACTED_IN]-(p:Player)-[:HAS_STATS]->(ps:PlayerStats)
                        WITH c, p, ps
                        ORDER BY ps.goals DESC
                        WITH c, COLLECT({player: p, stats: ps})[0] AS topScorer
                        RETURN c.name AS Country, topScorer.player.name AS TopScorer, topScorer.stats.goals AS Goals;
                        """,
                        """
                        MATCH (l:League)<-[:ACTED_IN]-(p:Player)-[:HAS_STATS]->(ps:PlayerStats)
                        RETURN l.name AS League, SUM(ps.mins) AS TotalMinutesPlayed
                        ORDER BY League, TotalMinutesPlayed DESC;
                        """,
                        """
                        MATCH (clb:Club)<-[:PLAYS_FOR]-(p:Player)-[:HAS_STATS]->(ps:PlayerStats)
                        WHERE clb.name IS NOT NULL AND clb.name <> 'None'
                        RETURN clb.name AS Club, SUM(ps.goals) AS TotalGoals
                        ORDER BY TotalGoals DESC
                        LIMIT 5;
                        """,
                        """
                        MATCH (c:Country)<-[:IN_COUNTRY]-(l:League)<-[:ACTED_IN]-(p:Player)
                        WITH c, p, COUNT(DISTINCT l) AS LeaguesPlayed
                        RETURN c.name AS Country, COUNT(DISTINCT p) AS TotalPlayers
                        ORDER BY TotalPlayers DESC;
                        """,
                        """
                        MATCH (l:League)<-[:ACTED_IN]-(p:Player)-[:HAS_STATS]->(ps:PlayerStats)
                        WITH l, SUM(ps.xG) AS TotalxG
                        RETURN l.name AS League, TotalxG
                        ORDER BY TotalxG DESC
                        LIMIT 5;
                        """
                        ]
    
    def data(self):
        self.driver.session().run(
            "LOAD CSV WITH HEADERS FROM 'file:///Data.csv' AS row  \
            MERGE (c:Country {name: row.Country}) \
            MERGE (l:League {name: row.League}) \
            MERGE (clb:Club {name: row.Club}) \
            MERGE (p: Player {name: row.`Player Names`}) \
            \
            MERGE (ps: PlayerStats {matchesPlayed: toInteger(row.Matches_Played), mins: toInteger(row.Mins), goals: toInteger(row.Goals), xG: toFloat(row.xG), xGPerAvgMatch: toFloat(row.`xG Per Avg Match`)}) \
            \
            MERGE (p)-[:PLAYS_FOR]->(cl) \
            MERGE (p)-[:ACTED_IN]->(l) \
            MERGE (l)-[:IN_COUNTRY]->(c) \
            MERGE (p)-[:HAS_STATS]->(ps)")

    def queries(self):
        f = open("CBD_L44c_output.txt", "w")
        for i in range(len(self.answers)):
            query = self.answers[i]
            f.write("\nQuery: {}\n".format(self.questions[i]))
            res = self.driver.session().run(query)

            results=[r for r in res.data()]
            
            for r in results:
                f.write(f"\t{r}\n")
            
    def close(self):
        self.driver.close()




if __name__ == "__main__":
    try:
        neo4jBD = lab4_4("bolt://localhost:7687", "neo4j", "password")
        neo4jBD.data()
        print("Connected to Neo4j, entities created and relationships established.")
        neo4jBD.queries()
        print("Queries executed and results saved in CBD_L44c_output.txt")
        neo4jBD.close()
    except Exception as e:
        print("There was an error!", e)        