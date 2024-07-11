import React, {useEffect, useState} from "react";
import globe_icon from "../../../../../images/globe_icon.png";
import sports_icon from "../../../../../images/sports_icon.png";
import culture_icon from "../../../../../images/culture_icon.png";
import education_icon from "../../../../../images/education_icon.png";
import food_icon from "../../../../../images/food_icon.png";
import charity_icon from "../../../../../images/charity_icon.png";
import technology_icon from "../../../../../images/technology_icon.png";
import business_icon from "../../../../../images/business_icon.png";
import {getCategories, searchEvents} from "../../../../services/eventsServices";

const icons = [
    technology_icon,
    culture_icon,
    sports_icon,
    business_icon,
    food_icon,
    education_icon,
    charity_icon
]

export function CategoryList({setPageEvents, currentPage, itemsPerPage, setIsLoading, setError}) {
    const [categoryIcons, setCategoryIcons] = useState({
        "All": globe_icon
    });

    useEffect(() => {
        getCategories()
            .then((res) => {
                if (res.data.error) setError(res.data.error);
                else {
                    const newIcons = {};
                    res.data.forEach((category, index) => {
                        if (category == "Simple Meeting") return;
                        newIcons[category] = icons[index % icons.length]
                    });
                    setCategoryIcons({ ...categoryIcons, ...newIcons });
                    setError('')
                }
            });
    }, []);

    const handleCategoryClick = (category: string) => {
        setIsLoading(true)
        searchEvents(category, itemsPerPage, currentPage * itemsPerPage - itemsPerPage)
            .then((res) => {
                if (res.data.error) setError(res.data.error);
                else {
                    if (category === "All") setPageEvents(res.data.events);
                    else setPageEvents(res.data.events.filter(event => event.visibility === "Public"));
                    setError('')
                }
                setIsLoading(false)
            });
    }

    return (
        <div className="category-scroll">
            {Object.keys(categoryIcons).map(category => (
                <button key={category} onClick={() => handleCategoryClick(category)}>
                    <img src={categoryIcons[category]} alt={category} title={category} className={"images"}/>
                </button>
            ))}
        </div>
    )
}