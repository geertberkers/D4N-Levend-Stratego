using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class Animal {

    public Animal Prey1;
    public Animal Prey2;
    public Animal Hunter1;
    public Animal Hunter2;

    public Sprite Image;

    public Animal()
    {

    }

    public Animal(Sprite image)
    {
        this.Image = image;
    }

    public Animal(Animal prey1, Animal prey2, Animal hunter1, Animal hunter2)
    {
        this.Prey1 = prey1;
        this.Prey2 = prey2;
        this.Hunter1 = hunter1;
        this.Hunter2 = hunter2;
    }
}
