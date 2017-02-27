package com.frankfenton.deeperthinkserver

import com.frankfenton.deeperthinkserver.models.Label
import com.frankfenton.deeperthinkserver.models.Leaf
import com.frankfenton.deeperthinkserver.models.Tag
import com.mongodb.WriteConcern
import groovy.json.JsonSlurper
import groovyx.gpars.GParsPool
import org.mongodb.morphia.InsertOptions

/**
 * Created by EchelonFour on 23/02/2017.
 */
class Main {
    static jsonSlurper = new JsonSlurper()
    static store = MongoStore.store


    static loadToDB() {
        //delete all
        store.delete(store.createQuery(Leaf.class))
        GParsPool.withPool {
            for (sentence in new File('./training').listFiles(new FileFilter() {
                @Override
                boolean accept(File pathname) {
                    pathname.name.endsWith('.json')
                }
            })) {
                def quote = jsonSlurper.parse(sentence)
                def metaRoot = new Leaf(tag: Tag.METAROOT, label: Label.METAROOT)
                List<Leaf> leafs = []
                for (token in quote.tokens) {
                    leafs.add(new Leaf(word: token.text.content.toLowerCase(), tag: token.partOfSpeech.tag, label: token.dependencyEdge.label, lemma: token.lemma.toLowerCase()))
                }
                quote.tokens.eachWithIndex { token, int i ->
                    int parentId = token.dependencyEdge.headTokenIndex
                    if (parentId == i) {
                        metaRoot.addToLeft(leafs[i])
                    }
                    if (parentId > i) {
                        leafs[parentId].addToLeft(leafs[i])
                    }
                    if (parentId < i) {
                        leafs[parentId].addToRight(leafs[i])
                    }
                }
                metaRoot.calculateTreeSize()
                leafs.add(metaRoot)

                GParsPool.executeAsync {
                    store.save(leafs, new InsertOptions().writeConcern(WriteConcern.ACKNOWLEDGED))
                    println("Saved ${leafs.size()} leafs")
                }
            }
        }
    }

    public static void main(String[] args) {
        if (!args.contains('noreload')) {
            loadToDB()
        }
        while (1) {
            Leaf phrase = null
            while(!phrase || phrase.calculateTreeSize() > 30) {
                //need to rework this to get it right the first time.
                phrase = Randos.randoTree
            }
            def text = phrase.toSentence()
            println text
            Firebase.instance.currentPhrase = text
            sleep(20 * 1000)
        }

    }
}
