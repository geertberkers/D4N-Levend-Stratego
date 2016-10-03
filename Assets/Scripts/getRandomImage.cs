using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class getRandomImage : MonoBehaviour {

    public Image Stone;
    public Image Scissors;
    public Image Paper;

	// Use this for initialization
	void Start () {
        Stone.enabled = false;
        Scissors.enabled = false;
        Paper.enabled = false;
    }
	
	// Update is called once per frame
	void Update () {
	
	}

    public void enableRandomPic()
    {
        int rand = UnityEngine.Random.Range(0, 3);
        if(rand == 0)
        {
            Stone.enabled = true;
            Scissors.enabled = false;
            Paper.enabled = false;
        }
        else if(rand == 1)
        {
            Stone.enabled = false;
            Scissors.enabled = true;
            Paper.enabled = false;
        }
        else
        {
            Stone.enabled = false;
            Scissors.enabled = false;
            Paper.enabled = true;
        }
    }
}
