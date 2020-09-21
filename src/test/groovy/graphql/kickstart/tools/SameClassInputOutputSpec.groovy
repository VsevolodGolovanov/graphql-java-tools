package graphql.kickstart.tools

import graphql.GraphQL
import graphql.execution.AsyncExecutionStrategy
import graphql.schema.GraphQLSchema
import spock.lang.Specification

class SameClassInputOutputSpec extends Specification {

    def "same class can be used for input and output"() {
        when:
        GraphQLSchema schema = SchemaParser.newParser().schemaString('''\
                        type Query {
                            read: Material!
                        }

                        type Mutation {
                            update(material: MaterialInput!): Material!
                        }
                        
                        type Material {
                            id: ID!
                            kind: MaterialKind!
                        }

                        type MaterialKind {
                            name: String!
                        }

                        input MaterialInput {
                            id: ID!
                            kind: MaterialKindInput!
                        }

                        type MaterialKindInput {
                            name: String!
                        }
                        
                    ''').resolvers(new QueryResolver(), new MutationResolver())
                .build()
                .makeExecutableSchema()
        GraphQL gql = GraphQL.newGraphQL(schema)
                .queryExecutionStrategy(new AsyncExecutionStrategy())
                .build()
        def data = Utils.assertNoGraphQlErrors(gql, [material: [id: "17", kind: [name: "Resin"]]]) {
            '''
                mutation update($material: MaterialInput!) {
                    update(material: $material) {
                        id
                        kind {
                            name 
                        }
                    }
                }
                '''
        }

        then:
        noExceptionThrown()
        data.update.kind.name == "Wood"
    }

    class QueryResolver implements GraphQLQueryResolver {
        Material read() { newMaterial() }
    }

    class MutationResolver implements GraphQLMutationResolver {
        Material update(Material material) { newMaterial() }
    }

    static class Material {
        Long id
        MaterialKind kind
    }

    static class MaterialKind {
        String name
    }

    static Material newMaterial() {
        MaterialKind kind = new MaterialKind()
        kind.name = "Wood"
        Material material = new Material()
        material.id = (Math.random() * 100).longValue()
        material.kind = kind
        return material
    }

}
