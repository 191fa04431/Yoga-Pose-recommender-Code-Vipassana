# Yoga-Pose-recommender-Code-Vipassana

With Google Cloud Firestore, we can develop rich mobile, web and IoT applications using a fully managed, scalable, and serverless No-SQL document database that horizontally scales to meet any demand, with no maintenance. Some of my favorite features of Firestore besides the fact that it is fully managed, serverless and effortlessly auto scales up or down to meet any demand with no maintenance windows or downtime are that it has Built-in live synchronization and offline mode makes it easy to build multi-user, collaborative applications Powerful query engine that allows you to run ACID compliant transactions whilst providing flexibility in structure for your data High availability of 99.99–99.999% achieved through strongly consistent data replication Lets you pay only for what you use, no up-front expenditure or underutilized resources Simplified architecture lets your apps talk directly to Firestore from your mobile or web clients For more detailed features and documentation, refer to product documentation.

Demonstrating CRUD and query operations on a client app using Firestore APIs: We are going to experiment with Firestore Setup, Queries, Indexes, CRUD operations using Firestore APIs on a Java Spring Boot application deployed on Cloud Run without using a Dockerfile. For this experiment, I have taken the use case of “Yoga posture and breathing manager” database to accomplish the following: (However, the full implementation of the app’s use case is not in scope for this blog)

What you'll do:

Design, Build and Deploy a web application that employs Vector Search to recommend Yoga poses.

What you'll learn:

How to use Gemini to generate text content and within the context of this codelab, generate descriptions for yoga poses
How to use Langchain Document Loader for Firestore to load records from an enhanced dataset from Hugging Face into Firestore along with Vector Embeddings
How to use Langchain Vector Store for Firestore to search for data based on a natural language query
How to use Google Cloud Text to Speech API to generate Audio content

What you'll need:

Chrome web browser
A Gmail account
A Cloud Project with billing enabled

You can refer to Google codelab for reference:

1. Java ( https://codelabs.developers.google.com/codelabs/smart-yoga-pose-search-firestore#3)
2. Python ( https://codelabs.developers.google.com/yoga-pose-firestore-vectorsearch-python#0)

Finally, you've successfully built an application that uploads a dataset to Firestore, generates the embeddings and does a Vector Similarity Search based on the user's query.

Thank you! If you found this helpful, please star and fork the repository.
